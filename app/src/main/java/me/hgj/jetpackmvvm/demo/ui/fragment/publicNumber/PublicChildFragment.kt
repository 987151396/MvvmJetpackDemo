package me.hgj.jetpackmvvm.demo.ui.fragment.publicNumber

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.kingja.loadsir.core.LoadService
import com.yanzhenjie.recyclerview.SwipeRecyclerView
import kotlinx.android.synthetic.main.include_list.*
import kotlinx.android.synthetic.main.include_recyclerview.*
import me.hgj.jetpackmvvm.demo.R
import me.hgj.jetpackmvvm.demo.app.appViewModel
import me.hgj.jetpackmvvm.demo.app.base.BaseFragment
import me.hgj.jetpackmvvm.demo.app.eventViewModel
import me.hgj.jetpackmvvm.demo.app.ext.*
import me.hgj.jetpackmvvm.demo.app.weight.recyclerview.DefineLoadMoreView
import me.hgj.jetpackmvvm.demo.app.weight.recyclerview.SpaceItemDecoration
import me.hgj.jetpackmvvm.demo.data.model.bean.CollectBus
import me.hgj.jetpackmvvm.demo.databinding.IncludeListBinding
import me.hgj.jetpackmvvm.demo.ui.adapter.AriticleAdapter
import me.hgj.jetpackmvvm.demo.viewmodel.request.RequestCollectViewModel
import me.hgj.jetpackmvvm.demo.viewmodel.request.RequestPublicNumberViewModel
import me.hgj.jetpackmvvm.demo.viewmodel.state.PublicNumberViewModel
import me.hgj.jetpackmvvm.ext.nav
import me.hgj.jetpackmvvm.ext.navigateAction

/**
 * 作者　: hegaojian
 * 时间　: 2020/2/29
 * 描述　: 公众号
 */
class PublicChildFragment : BaseFragment<PublicNumberViewModel, IncludeListBinding>() {

    //适配器
    private val articleAdapter: AriticleAdapter by lazy { AriticleAdapter(arrayListOf()) }

    //界面状态管理者
    private lateinit var loadsir: LoadService<Any>

    //recyclerview的底部加载view 因为在首页要动态改变他的颜色，所以加了他这个字段
    private lateinit var footView: DefineLoadMoreView

    //该项目对应的id
    private var cid = 0

    //收藏viewmodel
    private val requestCollectViewModel: RequestCollectViewModel by viewModels()

    //请求viewmodel
    private val requestPublicNumberViewModel: RequestPublicNumberViewModel by viewModels()

    override fun initView(savedInstanceState: Bundle?) {
        arguments?.let {
            cid = it.getInt("cid")
        }
        //状态页配置
        loadsir = loadServiceInit(swipeRefresh) {
            //点击重试时触发的操作
            loadsir.showLoading()
            requestPublicNumberViewModel.getPublicData(true, cid)
        }
        //初始化recyclerView
        recyclerView.init(LinearLayoutManager(context), articleAdapter).let {
            it.addItemDecoration(SpaceItemDecoration(0, ConvertUtils.dp2px(8f)))
            footView = it.initFooter(SwipeRecyclerView.LoadMoreListener {
                //触发加载更多时请求数据
                requestPublicNumberViewModel.getPublicData(false, cid)
            })
            //初始化FloatingActionButton
            it.initFloatBtn(floatbtn)
        }

        //初始化 SwipeRefreshLayout
        swipeRefresh.init {
            //触发刷新监听时请求数据
            requestPublicNumberViewModel.getPublicData(true, cid)
        }

        articleAdapter.run {
            setCollectClick { item, v, position ->
                if (v.isChecked) {
                    requestCollectViewModel.uncollect(item.id)
                } else {
                    requestCollectViewModel.collect(item.id)
                }
            }
            setOnItemClickListener { _, view, position ->
                nav().navigateAction(R.id.action_to_webFragment, Bundle().apply {
                    putParcelable("ariticleData", articleAdapter.data[position])
                })
            }
            addChildClickViewIds(R.id.item_home_author, R.id.item_project_author)
            setOnItemChildClickListener { _, view, position ->
                when (view.id) {
                    R.id.item_home_author, R.id.item_project_author -> {
                        nav().navigateAction(
                            R.id.action_mainfragment_to_lookInfoFragment,
                            Bundle().apply {
                                putInt("id", articleAdapter.data[position].userId)
                            })
                    }
                }
            }
        }
    }

    override fun lazyLoadData() {
        loadsir.showLoading()
        requestPublicNumberViewModel.getPublicData(true, cid)
    }

    override fun createObserver() {
        requestPublicNumberViewModel.publicDataState.observe(viewLifecycleOwner, Observer {
            //设值 新写了个拓展函数，搞死了这个恶心的重复代码
            loadListData(it, articleAdapter, loadsir, recyclerView, swipeRefresh)
        })
        requestCollectViewModel.collectUiState.observe(viewLifecycleOwner, Observer {
            if (it.isSuccess) {
                eventViewModel.collectEvent.value =  CollectBus(it.id, it.collect)

            } else {
                showMessage(it.errorMsg)
                for (index in articleAdapter.data.indices) {
                    if (articleAdapter.data[index].id == it.id) {
                        articleAdapter.data[index].collect = it.collect
                        articleAdapter.notifyItemChanged(index)
                        break
                    }
                }
            }
        })
        appViewModel.run {
            //监听账户信息是否改变 有值时(登录)将相关的数据设置为已收藏，为空时(退出登录)，将已收藏的数据变为未收藏
            userInfo.observeInFragment(this@PublicChildFragment, Observer {
                if (it != null) {
                    it.collectIds.forEach { id ->
                        for (item in articleAdapter.data) {
                            if (id.toInt() == item.id) {
                                item.collect = true
                                break
                            }
                        }
                    }
                } else {
                    for (item in articleAdapter.data) {
                        item.collect = false
                    }
                }
                articleAdapter.notifyDataSetChanged()
            })
            //监听全局的主题颜色改变
            appColor.observeInFragment(this@PublicChildFragment, Observer {
                setUiTheme(it, floatbtn, swipeRefresh, loadsir, footView)
            })
            //监听全局的列表动画改编
            appAnimation.observeInFragment(this@PublicChildFragment, Observer {
                articleAdapter.setAdapterAnimation(it)
            })
            //监听全局的收藏信息 收藏的Id跟本列表的数据id匹配则需要更新
            eventViewModel.collectEvent.observeInFragment(this@PublicChildFragment, Observer {
                for (index in articleAdapter.data.indices) {
                    if (articleAdapter.data[index].id == it.id) {
                        articleAdapter.data[index].collect = it.collect
                        articleAdapter.notifyItemChanged(index)
                        break
                    }
                }
            })
        }
    }

    companion object {
        fun newInstance(cid: Int): PublicChildFragment {
            val args = Bundle()
            args.putInt("cid", cid)
            val fragment = PublicChildFragment()
            fragment.arguments = args
            return fragment
        }
    }
}